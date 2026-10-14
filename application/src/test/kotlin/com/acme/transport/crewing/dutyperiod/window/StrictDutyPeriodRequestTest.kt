package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDutyPeriodRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictDutyPeriodRequest("code-579", "name-980")
        assertEquals(StrictDutyPeriodRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictDutyPeriodRequest("code-579", "name-980")
        try {
            item.moveTo(StrictDutyPeriodRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDutyPeriodRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictDutyPeriodRequest("code-579", "name-980")
        item.status = StrictDutyPeriodRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictDutyPeriodRequestStatus.PROPOSED) }
    }
}
