import { useEffect, useMemo, useState } from 'react'
import { appointmentsApi, routineSettingsApi } from './api'
import type { Appointment, AppointmentPayload, Recommendation, RoutineSettings, Weekday } from './types'

const weekdays: { value: Weekday; label: string }[] = [
  { value: 'MONDAY', label: 'Seg' }, { value: 'TUESDAY', label: 'Ter' },
  { value: 'WEDNESDAY', label: 'Qua' }, { value: 'THURSDAY', label: 'Qui' },
  { value: 'FRIDAY', label: 'Sex' }, { value: 'SATURDAY', label: 'Sáb' }, { value: 'SUNDAY', label: 'Dom' },
]

const emptyForm = (): AppointmentPayload => ({
  name: '', startTime: '19:00', date: null, destinationName: '', destinationPlaceId: null,
  destinationLatitude: null, destinationLongitude: null, recurring: true, recurringDays: ['MONDAY'],
})

const emptyRoutineSettings = (): RoutineSettings => ({
  homeName: '', homePlaceId: null, homeLatitude: null, homeLongitude: null,
  preparationMinutes: 30, safetyMarginMinutes: 10,
})

const dayLabel = (day: Weekday) => weekdays.find((item) => item.value === day)?.label ?? day

function App() {
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [selected, setSelected] = useState<Appointment | null>(null)
  const [form, setForm] = useState<AppointmentPayload>(emptyForm)
  const [isEditorOpen, setEditorOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [routineSettings, setRoutineSettings] = useState<RoutineSettings>(emptyRoutineSettings)
  const [isRoutineEditorOpen, setRoutineEditorOpen] = useState(false)
  const [recommendation, setRecommendation] = useState<Recommendation | null>(null)
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

  useEffect(() => {
    void loadAppointments()
    void routineSettingsApi.get().then(setRoutineSettings).catch(() => undefined)
  }, [])

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

  async function saveRoutineSettings(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    try {
      setSaving(true)
      setRoutineSettings(await routineSettingsApi.save(routineSettings))
      setRoutineEditorOpen(false)
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível salvar sua rotina.')
    } finally {
      setSaving(false)
    }
  }

  async function calculateRecommendation(appointment: Appointment) {
    try {
      setSaving(true)
      setError('')
      setRecommendation(await appointmentsApi.recommendation(appointment.id, nextOccurrenceStart(appointment)))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Não foi possível calcular a rota.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Weather or Not, início"><span>◒</span> weather or not</a>
        <div className="row-actions"><button className="button button-secondary" onClick={() => setRoutineEditorOpen(true)}>Minha rotina</button><button className="button button-primary" onClick={openCreate}>+ Novo compromisso</button></div>
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
          <p className="appointment-meta">{nextAppointment.startTime.slice(0, 5)} · {nextAppointment.destinationName}</p>
          <div className="recommendation-grid">{recommendation ? <><div><span>COMEÇAR A SE PREPARAR</span><strong>{formatTime(recommendation.preparationTime)}</strong></div><div><span>SAIR DE CASA</span><strong>{formatTime(recommendation.departureTime)}</strong></div></> : <><div><span>RECOMENDAÇÃO</span><strong>Calcule sua rota</strong></div><div><span>TRÂNSITO</span><strong>Consultado sob demanda</strong></div></>}</div>
          <p className="muted">{recommendation ? recommendation.reason : 'O backend calcula a rota entre sua origem e este destino usando o Google Maps.'}</p>
          <button className="text-button" disabled={saving} onClick={() => void calculateRecommendation(nextAppointment)}>Calcular recomendação →</button>
        </> : <div className="empty-hero"><h2 id="next-title">Sua agenda está livre</h2><p>Cadastre um compromisso para começar a planejar sua rotina.</p><button className="text-button" onClick={openCreate}>Cadastrar agora →</button></div>}
      </section>

      <section className="agenda-section" aria-labelledby="agenda-title">
        <div className="section-heading"><div><p className="eyebrow">SUA AGENDA</p><h2 id="agenda-title">Próximos compromissos</h2></div><button className="text-button" onClick={openCreate}>Adicionar +</button></div>
        {loading ? <p className="loading">Carregando agenda…</p> : appointments.length === 0 ? <p className="loading">Nenhum compromisso cadastrado.</p> : <div className="appointment-list">
          {appointments.map((appointment) => <article className="appointment-row" key={appointment.id}>
            <time>{appointment.startTime.slice(0, 5)}</time>
            <div><h3>{appointment.name}</h3><p>{appointment.recurring ? appointment.recurringDays.map(dayLabel).join(', ') : formatDate(appointment.date)} · {appointment.destinationName}</p></div>
            <div className="row-actions"><button onClick={() => openEdit(appointment)}>Editar</button><button className="danger" onClick={() => void remove(appointment)}>Excluir</button></div>
          </article>)}
        </div>}
      </section>

      {isEditorOpen && <div className="dialog-backdrop" role="presentation"><section className="editor" role="dialog" aria-modal="true" aria-labelledby="editor-title">
        <div className="editor-heading"><div><p className="eyebrow">{selected ? 'ATUALIZAR AGENDA' : 'NOVA ROTINA'}</p><h2 id="editor-title">{selected ? 'Editar compromisso' : 'Adicionar compromisso'}</h2></div><button className="close" aria-label="Fechar" onClick={() => setEditorOpen(false)}>×</button></div>
        <form onSubmit={(event) => void save(event)}>
          <div className="form-grid"><label>Nome<input required value={form.name} onChange={(event) => updateField('name', event.target.value)} placeholder="Ex.: Faculdade" /></label><label>Horário<input required type="time" value={form.startTime} onChange={(event) => updateField('startTime', event.target.value)} /></label><label>Destino<input required value={form.destinationName} onChange={(event) => updateField('destinationName', event.target.value)} placeholder="Ex.: Universidade" /></label><label>Google Place ID <span>(por enquanto)</span><input value={form.destinationPlaceId ?? ''} onChange={(event) => updateField('destinationPlaceId', event.target.value || null)} placeholder="ChIJ..." /></label></div>
          <fieldset><legend>Repetição</legend><label className="switch-line"><input type="checkbox" checked={form.recurring} onChange={(event) => updateField('recurring', event.target.checked)} /> Repetir semanalmente</label>{form.recurring ? <div className="day-picker">{weekdays.map((day) => <button type="button" key={day.value} className={form.recurringDays.includes(day.value) ? 'selected' : ''} onClick={() => toggleDay(day.value)}>{day.label}</button>)}</div> : <label>Data<input required type="date" value={form.date ?? ''} onChange={(event) => updateField('date', event.target.value || null)} /></label>}</fieldset>
          {error && <p className="form-error" role="alert">{error}</p>}<div className="form-actions"><button type="button" className="button button-secondary" onClick={() => setEditorOpen(false)}>Cancelar</button><button className="button button-primary" disabled={saving}>{saving ? 'Salvando…' : 'Salvar compromisso'}</button></div>
        </form>
      </section></div>}
      {isRoutineEditorOpen && <div className="dialog-backdrop" role="presentation"><section className="editor" role="dialog" aria-modal="true" aria-labelledby="routine-editor-title">
        <div className="editor-heading"><div><p className="eyebrow">SUA ROTINA</p><h2 id="routine-editor-title">Origem e tempos</h2></div><button className="close" aria-label="Fechar" onClick={() => setRoutineEditorOpen(false)}>×</button></div>
        <form onSubmit={(event) => void saveRoutineSettings(event)}>
          <div className="form-grid"><label>Origem<input required value={routineSettings.homeName} onChange={(event) => setRoutineSettings((current) => ({ ...current, homeName: event.target.value }))} placeholder="Ex.: Minha casa" /></label><label>Google Place ID<input value={routineSettings.homePlaceId ?? ''} onChange={(event) => setRoutineSettings((current) => ({ ...current, homePlaceId: event.target.value || null }))} placeholder="ChIJ..." /></label><label>Preparação <span>(min)</span><input min="0" type="number" value={routineSettings.preparationMinutes} onChange={(event) => setRoutineSettings((current) => ({ ...current, preparationMinutes: Number(event.target.value) }))} /></label><label>Margem <span>(min)</span><input min="0" type="number" value={routineSettings.safetyMarginMinutes} onChange={(event) => setRoutineSettings((current) => ({ ...current, safetyMarginMinutes: Number(event.target.value) }))} /></label></div>
          <p className="muted">A busca automática de Place ID será adicionada com autocomplete; por ora informe o identificador ou configure coordenadas pela API.</p>
          <div className="form-actions"><button type="button" className="button button-secondary" onClick={() => setRoutineEditorOpen(false)}>Cancelar</button><button className="button button-primary" disabled={saving}>{saving ? 'Salvando…' : 'Salvar rotina'}</button></div>
        </form>
      </section></div>}
    </main>
  )
}

function nextOccurrenceStart(appointment: Appointment) {
  if (!appointment.recurring && appointment.date) return `${appointment.date}T${appointment.startTime}`
  const candidate = new Date()
  const [hour, minute] = appointment.startTime.split(':').map(Number)
  const weekdayByValue: Record<Weekday, number> = { MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6, SUNDAY: 0 }
  for (let offset = 0; offset < 7; offset += 1) {
    const date = new Date(candidate)
    date.setDate(candidate.getDate() + offset)
    date.setHours(hour, minute, 0, 0)
    if (appointment.recurringDays.some((day) => weekdayByValue[day] === date.getDay()) && date >= candidate) {
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T${appointment.startTime}`
    }
  }
  return `${candidate.getFullYear()}-${String(candidate.getMonth() + 1).padStart(2, '0')}-${String(candidate.getDate()).padStart(2, '0')}T${appointment.startTime}`
}

function formatTime(value: string) {
  return value.slice(11, 16)
}

function formatDate(date: string | null) {
  return date ? new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' }).format(new Date(`${date}T12:00:00`)) : 'Data não informada'
}

export default App
