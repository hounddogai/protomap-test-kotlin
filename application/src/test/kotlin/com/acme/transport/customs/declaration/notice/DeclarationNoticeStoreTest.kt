package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationNoticeStore()
        val item = newDeclarationNotice("code-895", "title-756")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
