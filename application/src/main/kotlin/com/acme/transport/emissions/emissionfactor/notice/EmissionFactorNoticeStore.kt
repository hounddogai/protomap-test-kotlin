package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor notices by code. */
interface EmissionFactorNoticeStore {
    fun find(code: String): EmissionFactorNotice?
    fun save(item: EmissionFactorNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorNoticeStore(
    var items: MutableMap<String, EmissionFactorNotice>,
    var order: MutableList<String>,
) : EmissionFactorNoticeStore {
    override fun find(code: String): EmissionFactorNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorNotice) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<EmissionFactorNotice> {
        val out = mutableListOf<EmissionFactorNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorNoticeStore(): EmissionFactorNoticeStore {
    return MemoryEmissionFactorNoticeStore(
        mutableMapOf<String, EmissionFactorNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorNoticeStore(
    var inner: EmissionFactorNoticeStore,
    var cache: TtlCache<EmissionFactorNotice>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorNoticeStore {
    override fun find(code: String): EmissionFactorNotice? {
        val hit = (cache.get(code) as EmissionFactorNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorNotice> = inner.list()
}

fun newCachedEmissionFactorNoticeStore(inner: EmissionFactorNoticeStore, ttl: Int, clock: Clock): EmissionFactorNoticeStore {
    return CachedEmissionFactorNoticeStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor notices. Empty fields match all. */
data class EmissionFactorNoticeQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorNotice>): MutableList<EmissionFactorNotice> {
        val matched = filter(items) { candidate: EmissionFactorNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorNotices(store: EmissionFactorNoticeStore, query: EmissionFactorNoticeQuery): MutableList<EmissionFactorNotice> {
    return query.apply(store.list())
}
