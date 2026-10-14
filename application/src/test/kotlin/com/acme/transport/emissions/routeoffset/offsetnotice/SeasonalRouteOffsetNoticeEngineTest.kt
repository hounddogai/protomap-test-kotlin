package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRouteOffsetNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRouteOffsetNoticeEngine()
        val item = newSeasonalRouteOffsetNotice("code-462", "name-357")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
