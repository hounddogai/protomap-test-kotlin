package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDeclarationVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDeclarationVarianceStore()
        val item = newPendingDeclarationVariance("code-382", "label-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
