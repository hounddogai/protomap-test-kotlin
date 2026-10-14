package com.acme.transport.emissions.idling.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingIdlingRequestTest {
    @Test
    fun `starts in created`() {
        val item = newRollingIdlingRequest("code-135", "title-262")
        assertEquals(RollingIdlingRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingIdlingRequest("code-135", "title-262")
        try {
            item.moveTo(RollingIdlingRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingIdlingRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingIdlingRequest("code-135", "title-262")
        item.status = RollingIdlingRequestStatus.RETIRED
        assertFails("move") { item.moveTo(RollingIdlingRequestStatus.CREATED) }
    }
}
