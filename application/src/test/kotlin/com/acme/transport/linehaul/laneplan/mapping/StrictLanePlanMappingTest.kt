package com.acme.transport.linehaul.laneplan.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLanePlanMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictLanePlanMapping("code-634", "title-459")
        assertEquals(StrictLanePlanMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictLanePlanMapping("code-634", "title-459")
        try {
            item.moveTo(StrictLanePlanMappingStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLanePlanMappingStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictLanePlanMapping("code-634", "title-459")
        item.status = StrictLanePlanMappingStatus.CLOSED
        assertFails("move") { item.moveTo(StrictLanePlanMappingStatus.DRAFT) }
    }
}
