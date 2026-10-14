package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalLanePlanReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalLanePlanReservationStore()
        val item = newSeasonalLanePlanReservation("code-819", "title-592")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
