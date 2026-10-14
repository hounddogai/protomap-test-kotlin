package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newRestBreakAgreement("code-402", "title-85")
        assertEquals(RestBreakAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRestBreakAgreement("code-402", "title-85")
        try {
            item.moveTo(RestBreakAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRestBreakAgreement("code-402", "title-85")
        item.status = RestBreakAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(RestBreakAgreementStatus.REQUESTED) }
    }
}
