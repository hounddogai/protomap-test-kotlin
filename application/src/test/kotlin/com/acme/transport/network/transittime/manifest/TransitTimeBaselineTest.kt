package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newTransitTimeBaseline("code-554", "name-336")
        assertEquals(TransitTimeBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTransitTimeBaseline("code-554", "name-336")
        try {
            item.moveTo(TransitTimeBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTransitTimeBaseline("code-554", "name-336")
        item.status = TransitTimeBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(TransitTimeBaselineStatus.CREATED) }
    }
}
