package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class JiebaAdapter implements Iterator<SegToken> {

  private final static JiebaSegmenter jiebaTagger = new JiebaSegmenter();

  private final SegMode segMode;

  private Iterator<SegToken> tokens;

  private String raw = null;

  //use a special comparator to order by startoffset asc and length(then endOffset) desc
  //which can fix a bug cause highlight
  private static final Comparator<SegToken> ComparatorSegToken = (s1, s2) -> {
    return (s1.startOffset != s2.startOffset)?(s1.startOffset-s2.startOffset):(s2.endOffset-s1.endOffset);
  };

  public JiebaAdapter(String segModeName) {


    System.out.println("init jieba adapter");
    if (null == segModeName) {
      segMode = SegMode.SEARCH;
    } else {
      segMode = SegMode.valueOf(segModeName);
    }
  }

  public synchronized void reset(Reader input) {
    try {
      StringBuilder bdr = new StringBuilder();
      char[] buf = new char[1024];
      int size = 0;
      while ((size = input.read(buf, 0, buf.length)) != -1) {
        String tempstr = new String(buf, 0, size);
        bdr.append(tempstr);
      }
      raw = bdr.toString().trim();
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<SegToken> list = jiebaTagger.process(raw, segMode);
    //compare by startoffset asc and endoffset desc
    Collections.sort(list,ComparatorSegToken);
    tokens = list.iterator();
  }

  @Override
  public boolean hasNext() {
    return tokens.hasNext();
  }

  @Override
  public SegToken next() {
    return tokens.next();
  }

  @Override
  public void remove() {
    tokens.remove();
  }
}