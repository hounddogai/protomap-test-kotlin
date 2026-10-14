package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default route offset agreements by code. */
interface DefaultRouteOffsetAgreementStore {
    fun find(code: String): DefaultRouteOffsetAgreement?
    fun save(item: DefaultRouteOffsetAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRouteOffsetAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRouteOffsetAgreementStore(
    var items: MutableMap<String, DefaultRouteOffsetAgreement>,
    var order: MutableList<String>,
) : DefaultRouteOffsetAgreementStore {
    override fun find(code: String): DefaultRouteOffsetAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRouteOffsetAgreement) {
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

    override fun list(): MutableList<DefaultRouteOffsetAgreement> {
        val out = mutableListOf<DefaultRouteOffsetAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRouteOffsetAgreementStore(): DefaultRouteOffsetAgreementStore {
    return MemoryDefaultRouteOffsetAgreementStore(
        mutableMapOf<String, DefaultRouteOffsetAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRouteOffsetAgreementStore(
    var inner: DefaultRouteOffsetAgreementStore,
    var cache: TtlCache<DefaultRouteOffsetAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultRouteOffsetAgreementStore {
    override fun find(code: String): DefaultRouteOffsetAgreement? {
        val hit = (cache.get(code) as DefaultRouteOffsetAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRouteOffsetAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRouteOffsetAgreement> = inner.list()
}

fun newCachedDefaultRouteOffsetAgreementStore(inner: DefaultRouteOffsetAgreementStore, ttl: Int, clock: Clock): DefaultRouteOffsetAgreementStore {
    return CachedDefaultRouteOffsetAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultRouteOffsetAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default route offset agreements. Empty fields match all. */
data class DefaultRouteOffsetAgreementQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRouteOffsetAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRouteOffsetAgreement>): MutableList<DefaultRouteOffsetAgreement> {
        val matched = filter(items) { candidate: DefaultRouteOffsetAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRouteOffsetAgreements(store: DefaultRouteOffsetAgreementStore, query: DefaultRouteOffsetAgreementQuery): MutableList<DefaultRouteOffsetAgreement> {
    return query.apply(store.list())
}
