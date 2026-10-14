package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledEmissionFactorRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledEmissionFactorRollup("code-410", "title-23")
        assertEquals(ScheduledEmissionFactorRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledEmissionFactorRollup("code-410", "title-23")
        try {
            item.moveTo(ScheduledEmissionFactorRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledEmissionFactorRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledEmissionFactorRollup("code-410", "title-23")
        item.status = ScheduledEmissionFactorRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledEmissionFactorRollupStatus.REQUESTED) }
    }
}
