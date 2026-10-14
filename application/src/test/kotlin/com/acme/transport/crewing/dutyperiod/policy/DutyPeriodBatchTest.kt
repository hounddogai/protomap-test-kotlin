package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newDutyPeriodBatch("code-60", "title-275")
        assertEquals(DutyPeriodBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDutyPeriodBatch("code-60", "title-275")
        try {
            item.moveTo(DutyPeriodBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDutyPeriodBatch("code-60", "title-275")
        item.status = DutyPeriodBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(DutyPeriodBatchStatus.REQUESTED) }
    }
}
