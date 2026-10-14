package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorNoticeStore()
        val item = newEmissionFactorNotice("code-819", "label-472")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
