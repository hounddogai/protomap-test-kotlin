package com.acme.transport.freightpay.accessorial.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAccessorialMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackAccessorialMilestone("code-784", "title-160")
        assertEquals(FallbackAccessorialMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackAccessorialMilestone("code-784", "title-160")
        try {
            item.moveTo(FallbackAccessorialMilestoneStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAccessorialMilestoneStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackAccessorialMilestone("code-784", "title-160")
        item.status = FallbackAccessorialMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackAccessorialMilestoneStatus.CREATED) }
    }
}
