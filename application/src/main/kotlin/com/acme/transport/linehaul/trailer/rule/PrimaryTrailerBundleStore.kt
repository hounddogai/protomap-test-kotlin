package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary trailer bundles by code. */
interface PrimaryTrailerBundleStore {
    fun find(code: String): PrimaryTrailerBundle?
    fun save(item: PrimaryTrailerBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTrailerBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTrailerBundleStore(
    var items: MutableMap<String, PrimaryTrailerBundle>,
    var order: MutableList<String>,
) : PrimaryTrailerBundleStore {
    override fun find(code: String): PrimaryTrailerBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTrailerBundle) {
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

    override fun list(): MutableList<PrimaryTrailerBundle> {
        val out = mutableListOf<PrimaryTrailerBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTrailerBundleStore(): PrimaryTrailerBundleStore {
    return MemoryPrimaryTrailerBundleStore(
        mutableMapOf<String, PrimaryTrailerBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTrailerBundleStore(
    var inner: PrimaryTrailerBundleStore,
    var cache: TtlCache<PrimaryTrailerBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryTrailerBundleStore {
    override fun find(code: String): PrimaryTrailerBundle? {
        val hit = (cache.get(code) as PrimaryTrailerBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTrailerBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTrailerBundle> = inner.list()
}

fun newCachedPrimaryTrailerBundleStore(inner: PrimaryTrailerBundleStore, ttl: Int, clock: Clock): PrimaryTrailerBundleStore {
    return CachedPrimaryTrailerBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryTrailerBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary trailer bundles. Empty fields match all. */
data class PrimaryTrailerBundleQuery(
    var locale: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTrailerBundle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTrailerBundle>): MutableList<PrimaryTrailerBundle> {
        val matched = filter(items) { candidate: PrimaryTrailerBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTrailerBundles(store: PrimaryTrailerBundleStore, query: PrimaryTrailerBundleQuery): MutableList<PrimaryTrailerBundle> {
    return query.apply(store.list())
}
