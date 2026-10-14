package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCameraEventPlanTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalCameraEventPlan("code-214", "title-873")
        assertEquals(SeasonalCameraEventPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newSeasonalCameraEventPlan("code-214", "title-873")
        try {
            item.moveTo(SeasonalCameraEventPlanStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCameraEventPlanStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalCameraEventPlan("code-214", "title-873")
        item.status = SeasonalCameraEventPlanStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalCameraEventPlanStatus.NEW) }
    }
}
