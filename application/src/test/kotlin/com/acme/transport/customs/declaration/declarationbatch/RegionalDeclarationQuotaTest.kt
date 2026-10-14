package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDeclarationQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        assertEquals(RegionalDeclarationQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        try {
            item.moveTo(RegionalDeclarationQuotaStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDeclarationQuotaStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        item.status = RegionalDeclarationQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalDeclarationQuotaStatus.DRAFT) }
    }
}
