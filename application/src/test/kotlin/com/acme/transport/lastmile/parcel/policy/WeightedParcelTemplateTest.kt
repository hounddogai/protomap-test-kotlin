package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedParcelTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedParcelTemplate("code-811", "label-394")
        assertEquals(WeightedParcelTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedParcelTemplate("code-811", "label-394")
        try {
            item.moveTo(WeightedParcelTemplateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedParcelTemplateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedParcelTemplate("code-811", "label-394")
        item.status = WeightedParcelTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedParcelTemplateStatus.PLANNED) }
    }
}
