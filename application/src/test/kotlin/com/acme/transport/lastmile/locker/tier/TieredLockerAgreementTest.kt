package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredLockerAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredLockerAgreement("code-208", "title-389")
        assertEquals(TieredLockerAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredLockerAgreement("code-208", "title-389")
        try {
            item.moveTo(TieredLockerAgreementStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredLockerAgreementStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredLockerAgreement("code-208", "title-389")
        item.status = TieredLockerAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(TieredLockerAgreementStatus.IDLE) }
    }
}
