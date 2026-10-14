package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultServiceAreaMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultServiceAreaMilestone("code-305", "title-942")
        assertEquals(DefaultServiceAreaMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultServiceAreaMilestone("code-305", "title-942")
        try {
            item.moveTo(DefaultServiceAreaMilestoneStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultServiceAreaMilestoneStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultServiceAreaMilestone("code-305", "title-942")
        item.status = DefaultServiceAreaMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(DefaultServiceAreaMilestoneStatus.NEW) }
    }
}
