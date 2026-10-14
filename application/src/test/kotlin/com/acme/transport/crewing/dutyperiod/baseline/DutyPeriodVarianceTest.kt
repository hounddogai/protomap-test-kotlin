package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodVariance("code-599", "name-403")
        assertEquals(DutyPeriodVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDutyPeriodVariance("code-599", "name-403")
        try {
            item.moveTo(DutyPeriodVarianceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodVarianceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodVariance("code-599", "name-403")
        item.status = DutyPeriodVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodVarianceStatus.PROPOSED) }
    }
}
