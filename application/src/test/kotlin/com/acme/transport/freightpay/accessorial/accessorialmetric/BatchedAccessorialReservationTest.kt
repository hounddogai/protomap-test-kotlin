package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAccessorialReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedAccessorialReservation("code-480", "title-243")
        assertEquals(BatchedAccessorialReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newBatchedAccessorialReservation("code-480", "title-243")
        try {
            item.moveTo(BatchedAccessorialReservationStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAccessorialReservationStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedAccessorialReservation("code-480", "title-243")
        item.status = BatchedAccessorialReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedAccessorialReservationStatus.PROPOSED) }
    }
}
