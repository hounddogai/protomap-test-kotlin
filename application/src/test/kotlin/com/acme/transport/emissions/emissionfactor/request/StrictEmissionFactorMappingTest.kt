package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEmissionFactorMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictEmissionFactorMapping("code-136", "title-699")
        assertEquals(StrictEmissionFactorMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newStrictEmissionFactorMapping("code-136", "title-699")
        try {
            item.moveTo(StrictEmissionFactorMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEmissionFactorMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictEmissionFactorMapping("code-136", "title-699")
        item.status = StrictEmissionFactorMappingStatus.STOPPED
        assertFails("move") { item.moveTo(StrictEmissionFactorMappingStatus.IDLE) }
    }
}
