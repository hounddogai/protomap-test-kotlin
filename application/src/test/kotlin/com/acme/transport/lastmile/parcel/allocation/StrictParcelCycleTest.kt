package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictParcelCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictParcelCycle("code-972", "name-643")
        assertEquals(StrictParcelCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newStrictParcelCycle("code-972", "name-643")
        try {
            item.moveTo(StrictParcelCycleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictParcelCycleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictParcelCycle("code-972", "name-643")
        item.status = StrictParcelCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictParcelCycleStatus.PROPOSED) }
    }
}
