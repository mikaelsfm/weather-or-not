export type Weekday =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export type Appointment = {
  id: number
  name: string
  type: string
  startTime: string
  date: string | null
  locationName: string | null
  latitude: number | null
  longitude: number | null
  preparationMinutes: number
  travelMinutes: number
  safetyMarginMinutes: number
  recurring: boolean
  recurringDays: Weekday[]
}

export type AppointmentPayload = Omit<Appointment, 'id'>
