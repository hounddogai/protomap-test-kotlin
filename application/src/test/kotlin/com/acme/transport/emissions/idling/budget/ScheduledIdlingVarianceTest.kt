package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledIdlingVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledIdlingVariance("code-826", "title-622")
        assertEquals(ScheduledIdlingVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledIdlingVariance("code-826", "title-622")
        try {
            item.moveTo(ScheduledIdlingVarianceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledIdlingVarianceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledIdlingVariance("code-826", "title-622")
        item.status = ScheduledIdlingVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledIdlingVarianceStatus.REQUESTED) }
    }
}
