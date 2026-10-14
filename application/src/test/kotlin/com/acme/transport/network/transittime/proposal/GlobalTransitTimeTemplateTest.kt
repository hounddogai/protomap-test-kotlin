package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTransitTimeTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalTransitTimeTemplate("code-680", "title-424")
        assertEquals(GlobalTransitTimeTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalTransitTimeTemplate("code-680", "title-424")
        try {
            item.moveTo(GlobalTransitTimeTemplateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTransitTimeTemplateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalTransitTimeTemplate("code-680", "title-424")
        item.status = GlobalTransitTimeTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalTransitTimeTemplateStatus.OPEN) }
    }
}
