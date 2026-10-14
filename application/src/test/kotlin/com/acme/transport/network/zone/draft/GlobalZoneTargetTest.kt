package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalZoneTargetTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalZoneTarget("code-917", "title-215")
        assertEquals(GlobalZoneTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalZoneTarget("code-917", "title-215")
        try {
            item.moveTo(GlobalZoneTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalZoneTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalZoneTarget("code-917", "title-215")
        item.status = GlobalZoneTargetStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalZoneTargetStatus.CREATED) }
    }
}
