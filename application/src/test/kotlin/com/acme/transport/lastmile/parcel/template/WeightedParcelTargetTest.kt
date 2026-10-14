package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedParcelTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedParcelTarget("code-973", "name-740")
        assertEquals(WeightedParcelTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newWeightedParcelTarget("code-973", "name-740")
        try {
            item.moveTo(WeightedParcelTargetStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedParcelTargetStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedParcelTarget("code-973", "name-740")
        item.status = WeightedParcelTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedParcelTargetStatus.PROPOSED) }
    }
}
