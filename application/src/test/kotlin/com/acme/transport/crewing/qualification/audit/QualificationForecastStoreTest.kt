package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationForecastStore()
        val item = newQualificationForecast("code-733", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
