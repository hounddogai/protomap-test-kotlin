package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDropNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDropNoticeStore()
        val item = newTieredDropNotice("code-377", "title-804")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
