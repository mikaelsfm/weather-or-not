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
  startTime: string
  date: string | null
  destinationName: string
  destinationPlaceId: string | null
  destinationLatitude: number | null
  destinationLongitude: number | null
  recurring: boolean
  recurringDays: Weekday[]
}

export type AppointmentPayload = Omit<Appointment, 'id'>

export type RoutineSettings = {
  homeName: string
  homePlaceId: string | null
  homeLatitude: number | null
  homeLongitude: number | null
  preparationMinutes: number
  safetyMarginMinutes: number
}

export type Recommendation = {
  preparationTime: string
  departureTime: string
  estimatedArrivalTime: string
  trafficImpactMinutes: number
  weatherImpactLevel: 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'SEVERE'
  weatherImpactMinutes: number
  reason: string
}
