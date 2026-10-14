package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss notices by code. */
interface NearMissNoticeStore {
    fun find(code: String): NearMissNotice?
    fun save(item: NearMissNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissNoticeStore(
    var items: MutableMap<String, NearMissNotice>,
    var order: MutableList<String>,
) : NearMissNoticeStore {
    override fun find(code: String): NearMissNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissNotice) {
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

    override fun list(): MutableList<NearMissNotice> {
        val out = mutableListOf<NearMissNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissNoticeStore(): NearMissNoticeStore {
    return MemoryNearMissNoticeStore(mutableMapOf<String, NearMissNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissNoticeStore(
    var inner: NearMissNoticeStore,
    var cache: TtlCache<NearMissNotice>,
    var hits: Int,
    var misses: Int,
) : NearMissNoticeStore {
    override fun find(code: String): NearMissNotice? {
        val hit = (cache.get(code) as NearMissNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissNotice> = inner.list()
}

fun newCachedNearMissNoticeStore(inner: NearMissNoticeStore, ttl: Int, clock: Clock): NearMissNoticeStore {
    return CachedNearMissNoticeStore(
        inner = inner,
        cache = newTtlCache<NearMissNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss notices. Empty fields match all. */
data class NearMissNoticeQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissNotice>): MutableList<NearMissNotice> {
        val matched = filter(items) { candidate: NearMissNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissNotices(store: NearMissNoticeStore, query: NearMissNoticeQuery): MutableList<NearMissNotice> {
    return query.apply(store.list())
}
