package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedLockerClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedLockerClaim("code-543", "name-245")
        val view = toBatchedLockerClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedLockerClaimViews(mutableListOf<BatchedLockerClaim>(item, item)).size, "views")
    }
}
