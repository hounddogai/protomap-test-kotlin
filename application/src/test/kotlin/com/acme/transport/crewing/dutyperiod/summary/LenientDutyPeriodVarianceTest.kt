package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDutyPeriodVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientDutyPeriodVariance("code-758", "title-382")
        assertEquals(LenientDutyPeriodVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientDutyPeriodVariance("code-758", "title-382")
        try {
            item.moveTo(LenientDutyPeriodVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDutyPeriodVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientDutyPeriodVariance("code-758", "title-382")
        item.status = LenientDutyPeriodVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientDutyPeriodVarianceStatus.PROPOSED) }
    }
}
