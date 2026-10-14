package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLanePlanSlotTest {
    @Test
    fun `starts in created`() {
        val item = newLenientLanePlanSlot("code-632", "title-930")
        assertEquals(LenientLanePlanSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientLanePlanSlot("code-632", "title-930")
        try {
            item.moveTo(LenientLanePlanSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLanePlanSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientLanePlanSlot("code-632", "title-930")
        item.status = LenientLanePlanSlotStatus.RETIRED
        assertFails("move") { item.moveTo(LenientLanePlanSlotStatus.CREATED) }
    }
}
