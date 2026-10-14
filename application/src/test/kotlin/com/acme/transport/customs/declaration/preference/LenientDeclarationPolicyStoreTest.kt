package com.acme.transport.customs.declaration.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDeclarationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDeclarationPolicyStore()
        val item = newLenientDeclarationPolicy("code-280", "name-209")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
