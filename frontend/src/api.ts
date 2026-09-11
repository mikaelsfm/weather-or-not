import type { Appointment, AppointmentPayload, Recommendation, RoutineSettings } from './types'

const baseUrl = import.meta.env.VITE_API_URL ?? ''

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })

  if (!response.ok) {
    const message = response.status === 404 ? 'Compromisso não encontrado.' : 'Não foi possível salvar suas alterações.'
    throw new Error(message)
  }

  return response.status === 204 ? (undefined as T) : response.json() as Promise<T>
}

export const appointmentsApi = {
  list: () => request<Appointment[]>('/appointments'),
  create: (payload: AppointmentPayload) => request<Appointment>('/appointments', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: number, payload: AppointmentPayload) => request<Appointment>(`/appointments/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  remove: (id: number) => request<void>(`/appointments/${id}`, { method: 'DELETE' }),
  recommendation: (id: number, start: string) => request<Recommendation>(`/appointments/${id}/recommendation?start=${encodeURIComponent(start)}`),
}

export const routineSettingsApi = {
  get: () => request<RoutineSettings>('/routine-settings'),
  save: (payload: RoutineSettings) => request<RoutineSettings>('/routine-settings', { method: 'PUT', body: JSON.stringify(payload) }),
}
