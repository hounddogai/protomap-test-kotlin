package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient trailer bundles by code. */
interface LenientTrailerBundleStore {
    fun find(code: String): LenientTrailerBundle?
    fun save(item: LenientTrailerBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTrailerBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTrailerBundleStore(
    var items: MutableMap<String, LenientTrailerBundle>,
    var order: MutableList<String>,
) : LenientTrailerBundleStore {
    override fun find(code: String): LenientTrailerBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTrailerBundle) {
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

    override fun list(): MutableList<LenientTrailerBundle> {
        val out = mutableListOf<LenientTrailerBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTrailerBundleStore(): LenientTrailerBundleStore {
    return MemoryLenientTrailerBundleStore(
        mutableMapOf<String, LenientTrailerBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTrailerBundleStore(
    var inner: LenientTrailerBundleStore,
    var cache: TtlCache<LenientTrailerBundle>,
    var hits: Int,
    var misses: Int,
) : LenientTrailerBundleStore {
    override fun find(code: String): LenientTrailerBundle? {
        val hit = (cache.get(code) as LenientTrailerBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTrailerBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTrailerBundle> = inner.list()
}

fun newCachedLenientTrailerBundleStore(inner: LenientTrailerBundleStore, ttl: Int, clock: Clock): LenientTrailerBundleStore {
    return CachedLenientTrailerBundleStore(
        inner = inner,
        cache = newTtlCache<LenientTrailerBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient trailer bundles. Empty fields match all. */
data class LenientTrailerBundleQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTrailerBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTrailerBundle>): MutableList<LenientTrailerBundle> {
        val matched = filter(items) { candidate: LenientTrailerBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTrailerBundles(store: LenientTrailerBundleStore, query: LenientTrailerBundleQuery): MutableList<LenientTrailerBundle> {
    return query.apply(store.list())
}
