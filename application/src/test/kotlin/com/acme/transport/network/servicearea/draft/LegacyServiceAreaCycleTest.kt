package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyServiceAreaCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyServiceAreaCycle("code-925", "title-326")
        assertEquals(LegacyServiceAreaCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyServiceAreaCycle("code-925", "title-326")
        try {
            item.moveTo(LegacyServiceAreaCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyServiceAreaCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyServiceAreaCycle("code-925", "title-326")
        item.status = LegacyServiceAreaCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyServiceAreaCycleStatus.PLANNED) }
    }
}
