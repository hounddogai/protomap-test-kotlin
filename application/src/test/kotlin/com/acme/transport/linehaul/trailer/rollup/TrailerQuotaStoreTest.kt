package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerQuotaStore()
        val item = newTrailerQuota("code-519", "title-868")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
