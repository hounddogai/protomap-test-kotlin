package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newLenientServiceAreaMilestone("code-377", "label-656")
        assertEquals(LenientServiceAreaMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientServiceAreaMilestone("code-377", "label-656")
        try {
            item.moveTo(LenientServiceAreaMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientServiceAreaMilestone("code-377", "label-656")
        item.status = LenientServiceAreaMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(LenientServiceAreaMilestoneStatus.CREATED) }
    }
}
