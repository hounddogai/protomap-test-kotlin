package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffTemplateStore()
        val item = newCutoffTemplate("code-638", "label-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
