package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLanePlanTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        assertEquals(LegacyLanePlanTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        try {
            item.moveTo(LegacyLanePlanTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLanePlanTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        item.status = LegacyLanePlanTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyLanePlanTargetStatus.REQUESTED) }
    }
}
