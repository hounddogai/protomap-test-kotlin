package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newLanePlanProfile("code-296", "label-924")
        assertEquals(LanePlanProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLanePlanProfile("code-296", "label-924")
        try {
            item.moveTo(LanePlanProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLanePlanProfile("code-296", "label-924")
        item.status = LanePlanProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(LanePlanProfileStatus.REQUESTED) }
    }
}
