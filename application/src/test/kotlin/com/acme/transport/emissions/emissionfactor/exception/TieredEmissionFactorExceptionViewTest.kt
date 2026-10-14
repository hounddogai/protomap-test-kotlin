package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredEmissionFactorExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredEmissionFactorException("code-997", "title-843")
        val view = toTieredEmissionFactorExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredEmissionFactorExceptionViews(mutableListOf<TieredEmissionFactorException>(item, item)).size, "views")
    }
}
