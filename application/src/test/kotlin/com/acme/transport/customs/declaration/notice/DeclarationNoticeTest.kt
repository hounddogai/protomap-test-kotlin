package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationNotice("code-895", "title-756")
        assertEquals(DeclarationNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDeclarationNotice("code-895", "title-756")
        try {
            item.moveTo(DeclarationNoticeStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationNoticeStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationNotice("code-895", "title-756")
        item.status = DeclarationNoticeStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationNoticeStatus.NEW) }
    }
}
