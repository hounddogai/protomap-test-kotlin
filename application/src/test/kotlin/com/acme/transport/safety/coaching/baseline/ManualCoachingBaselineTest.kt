package com.acme.transport.safety.coaching.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCoachingBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newManualCoachingBaseline("code-971", "name-519")
        assertEquals(ManualCoachingBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualCoachingBaseline("code-971", "name-519")
        try {
            item.moveTo(ManualCoachingBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCoachingBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualCoachingBaseline("code-971", "name-519")
        item.status = ManualCoachingBaselineStatus.FAILED
        assertFails("move") { item.moveTo(ManualCoachingBaselineStatus.NEW) }
    }
}
