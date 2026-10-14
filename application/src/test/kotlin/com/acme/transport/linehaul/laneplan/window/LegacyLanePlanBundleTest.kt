package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLanePlanBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyLanePlanBundle("code-73", "title-344")
        assertEquals(LegacyLanePlanBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyLanePlanBundle("code-73", "title-344")
        try {
            item.moveTo(LegacyLanePlanBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLanePlanBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyLanePlanBundle("code-73", "title-344")
        item.status = LegacyLanePlanBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyLanePlanBundleStatus.REQUESTED) }
    }
}
