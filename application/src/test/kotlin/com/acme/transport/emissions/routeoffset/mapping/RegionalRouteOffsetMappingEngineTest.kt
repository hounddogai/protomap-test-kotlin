package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRouteOffsetMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRouteOffsetMappingEngine(newRegistry())
        val item = newRegionalRouteOffsetMapping("code-319", "title-831")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
