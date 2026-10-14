package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaTemplateStore()
        val item = newServiceAreaTemplate("code-153", "title-659")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
