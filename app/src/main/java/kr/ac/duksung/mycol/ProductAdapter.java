package kr.ac.duksung.mycol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // 순위 설정
        holder.rank.setText(String.valueOf(position + 1));

        // 제품 이름과 옵션 이름 설정
        holder.productName.setText(product.getName());
        holder.optionName.setText(product.getOptionName());

        // Glide를 사용하여 이미지 로드
        Glide.with(context)
                .load(product.getImageUrl())
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView optionName;
        TextView rank;  // 순위 TextView 추가

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            optionName = itemView.findViewById(R.id.optionName);
            rank = itemView.findViewById(R.id.rank);  // 순위 TextView 연결
        }
    }
}
