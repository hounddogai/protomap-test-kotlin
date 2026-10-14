package com.acme.transport.crewing.swap.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newRollingSwapAgreement("code-326", "title-604")
        assertEquals(RollingSwapAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingSwapAgreement("code-326", "title-604")
        try {
            item.moveTo(RollingSwapAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingSwapAgreement("code-326", "title-604")
        item.status = RollingSwapAgreementStatus.FAILED
        assertFails("move") { item.moveTo(RollingSwapAgreementStatus.NEW) }
    }
}
