package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedLanePlanPreference("code-836", "title-761")
        assertEquals(CappedLanePlanPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCappedLanePlanPreference("code-836", "title-761")
        try {
            item.moveTo(CappedLanePlanPreferenceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanPreferenceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedLanePlanPreference("code-836", "title-761")
        item.status = CappedLanePlanPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedLanePlanPreferenceStatus.PLANNED) }
    }
}
