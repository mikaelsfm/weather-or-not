import { useEffect, useMemo, useState } from 'react'
import { appointmentsApi } from './api'
import type { Appointment, AppointmentPayload, Weekday } from './types'

const weekdays: { value: Weekday; label: string }[] = [
  { value: 'MONDAY', label: 'Seg' }, { value: 'TUESDAY', label: 'Ter' },
  { value: 'WEDNESDAY', label: 'Qua' }, { value: 'THURSDAY', label: 'Qui' },
  { value: 'FRIDAY', label: 'Sex' }, { value: 'SATURDAY', label: 'Sáb' }, { value: 'SUNDAY', label: 'Dom' },
]

const emptyForm = (): AppointmentPayload => ({
  name: '', type: '', startTime: '19:00', date: null, locationName: '', latitude: null, longitude: null,
  preparationMinutes: 30, travelMinutes: 30, safetyMarginMinutes: 10, recurring: true, recurringDays: ['MONDAY'],
})

const dayLabel = (day: Weekday) => weekdays.find((item) => item.value === day)?.label ?? day

function App() {
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [selected, setSelected] = useState<Appointment | null>(null)
  const [form, setForm] = useState<AppointmentPayload>(emptyForm)
  const [isEditorOpen, setEditorOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const nextAppointment = useMemo(() => appointments[0], [appointments])

  async function loadAppointments() {
    try {
      setLoading(true)
      setError('')
      setAppointments(await appointmentsApi.list())
    } catch {
      setError('Não foi possível conectar ao backend. Verifique se o Quarkus está em execução na porta 8080.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void loadAppointments() }, [])

  function openCreate() {
    setSelected(null)
    setForm(emptyForm())
    setError('')
    setEditorOpen(true)
  }

  function openEdit(appointment: Appointment) {
    setSelected(appointment)
    setForm({ ...appointment })
    setError('')
    setEditorOpen(true)
  }

  function updateField<Key extends keyof AppointmentPayload>(key: Key, value: AppointmentPayload[Key]) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  function toggleDay(day: Weekday) {
    const days = form.recurringDays.includes(day)
      ? form.recurringDays.filter((item) => item !== day)
      : [...form.recurringDays, day]
    updateField('recurringDays', days)
  }

  async function save(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (form.recurring && form.recurringDays.length === 0) {
      setError('Selecione pelo menos um dia para o compromisso recorrente.')
      return
    }
    if (!form.recurring && !form.date) {
      setError('Informe a data do compromisso único.')
      return
    }
    try {
      setSaving(true)
      setError('')
      const payload = { ...form, date: form.recurring ? null : form.date, recurringDays: form.recurring ? form.recurringDays : [] }
      if (selected) await appointmentsApi.update(selected.id, payload)
      else await appointmentsApi.create(payload)
      setEditorOpen(false)
      await loadAppointments()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível salvar o compromisso.')
    } finally {
      setSaving(false)
    }
  }

  async function remove(appointment: Appointment) {
    if (!window.confirm(`Excluir “${appointment.name}”?`)) return
    try {
      setError('')
      await appointmentsApi.remove(appointment.id)
      await loadAppointments()
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível excluir o compromisso.')
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Weather or Not, início"><span>◒</span> weather or not</a>
        <button className="button button-primary" onClick={openCreate}>+ Novo compromisso</button>
      </header>

      <section className="intro">
        <p className="eyebrow">SUA ROTINA, COM CLAREZA</p>
        <h1>O que você precisa fazer agora?</h1>
        <p>Organize seus compromissos. Em breve, o clima transformará esta agenda em recomendações de saída.</p>
      </section>

      {error && <div className="notice" role="alert">{error}</div>}

      <section className="hero-card" aria-labelledby="next-title">
        <div className="card-header"><p className="eyebrow">PRÓXIMO COMPROMISSO</p><span className="status">EM BREVE</span></div>
        {nextAppointment ? <>
          <h2 id="next-title">{nextAppointment.name}</h2>
          <p className="appointment-meta">{nextAppointment.type} · {nextAppointment.startTime.slice(0, 5)}{nextAppointment.locationName ? ` · ${nextAppointment.locationName}` : ''}</p>
          <div className="recommendation-grid">
            <div><span>COMEÇAR A SE PREPARAR</span><strong>{subtractMinutes(nextAppointment.startTime, nextAppointment.travelMinutes + nextAppointment.safetyMarginMinutes + nextAppointment.preparationMinutes)}</strong></div>
            <div><span>SAIR DE CASA</span><strong>{subtractMinutes(nextAppointment.startTime, nextAppointment.travelMinutes + nextAppointment.safetyMarginMinutes)}</strong></div>
          </div>
          <p className="muted">Horários calculados com o tempo normal de deslocamento e a margem de segurança.</p>
        </> : <div className="empty-hero"><h2 id="next-title">Sua agenda está livre</h2><p>Cadastre um compromisso para começar a planejar sua rotina.</p><button className="text-button" onClick={openCreate}>Cadastrar agora →</button></div>}
      </section>

      <section className="agenda-section" aria-labelledby="agenda-title">
        <div className="section-heading"><div><p className="eyebrow">SUA AGENDA</p><h2 id="agenda-title">Próximos compromissos</h2></div><button className="text-button" onClick={openCreate}>Adicionar +</button></div>
        {loading ? <p className="loading">Carregando agenda…</p> : appointments.length === 0 ? <p className="loading">Nenhum compromisso cadastrado.</p> : <div className="appointment-list">
          {appointments.map((appointment) => <article className="appointment-row" key={appointment.id}>
            <time>{appointment.startTime.slice(0, 5)}</time>
            <div><h3>{appointment.name}</h3><p>{appointment.type} · {appointment.recurring ? appointment.recurringDays.map(dayLabel).join(', ') : formatDate(appointment.date)}{appointment.locationName ? ` · ${appointment.locationName}` : ''}</p></div>
            <div className="row-actions"><button onClick={() => openEdit(appointment)}>Editar</button><button className="danger" onClick={() => void remove(appointment)}>Excluir</button></div>
          </article>)}
        </div>}
      </section>

      {isEditorOpen && <div className="dialog-backdrop" role="presentation"><section className="editor" role="dialog" aria-modal="true" aria-labelledby="editor-title">
        <div className="editor-heading"><div><p className="eyebrow">{selected ? 'ATUALIZAR AGENDA' : 'NOVA ROTINA'}</p><h2 id="editor-title">{selected ? 'Editar compromisso' : 'Adicionar compromisso'}</h2></div><button className="close" aria-label="Fechar" onClick={() => setEditorOpen(false)}>×</button></div>
        <form onSubmit={(event) => void save(event)}>
          <div className="form-grid"><label>Nome<input required value={form.name} onChange={(event) => updateField('name', event.target.value)} placeholder="Ex.: Faculdade" /></label><label>Tipo<input required value={form.type} onChange={(event) => updateField('type', event.target.value)} placeholder="Ex.: Aula" /></label><label>Horário<input required type="time" value={form.startTime} onChange={(event) => updateField('startTime', event.target.value)} /></label><label>Local<input value={form.locationName ?? ''} onChange={(event) => updateField('locationName', event.target.value)} placeholder="Ex.: Universidade" /></label></div>
          <fieldset><legend>Repetição</legend><label className="switch-line"><input type="checkbox" checked={form.recurring} onChange={(event) => updateField('recurring', event.target.checked)} /> Repetir semanalmente</label>{form.recurring ? <div className="day-picker">{weekdays.map((day) => <button type="button" key={day.value} className={form.recurringDays.includes(day.value) ? 'selected' : ''} onClick={() => toggleDay(day.value)}>{day.label}</button>)}</div> : <label>Data<input required type="date" value={form.date ?? ''} onChange={(event) => updateField('date', event.target.value || null)} /></label>}</fieldset>
          <fieldset><legend>Seu tempo</legend><div className="form-grid three"><label>Preparação <span>(min)</span><input min="0" type="number" value={form.preparationMinutes} onChange={(event) => updateField('preparationMinutes', Number(event.target.value))} /></label><label>Deslocamento <span>(min)</span><input min="0" type="number" value={form.travelMinutes} onChange={(event) => updateField('travelMinutes', Number(event.target.value))} /></label><label>Margem <span>(min)</span><input min="0" type="number" value={form.safetyMarginMinutes} onChange={(event) => updateField('safetyMarginMinutes', Number(event.target.value))} /></label></div></fieldset>
          {error && <p className="form-error" role="alert">{error}</p>}<div className="form-actions"><button type="button" className="button button-secondary" onClick={() => setEditorOpen(false)}>Cancelar</button><button className="button button-primary" disabled={saving}>{saving ? 'Salvando…' : 'Salvar compromisso'}</button></div>
        </form>
      </section></div>}
    </main>
  )
}

function subtractMinutes(time: string, minutes: number) {
  const [hour, minute] = time.split(':').map(Number)
  const date = new Date(2000, 0, 1, hour, minute - minutes)
  return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

function formatDate(date: string | null) {
  return date ? new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' }).format(new Date(`${date}T12:00:00`)) : 'Data não informada'
}

export default App
